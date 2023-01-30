require 'spec_helper'
require 'pry'

feature 'API Tokens', type: :feature do

  context 'signed in as an admin' do

    before :each do
      @admin = FactoryBot.create :admin
      sign_in_as @admin
    end

    let :add_api_token do
      visit '/my/user/me'
      click_on_first 'API-Tokens'
      click_on_first 'Add API-Token'
      fill_in 'Description', with: "My first token"
      click_on 'Add'
      wait_until{ page.has_content? "has been added"}
      @token_part = find(".token_part").text
      @token_secret = find(".token_secret").text
      click_on 'Continue'
      wait_until{ page.has_content? "API-Tokens"}
      expect(page).to have_content @token_part
      click_on_first @token_part
      wait_until(10){ all('.modal-body').empty? }
    end

    scenario 'creating an API-Token works' do
      add_api_token
    end


    context 'an API-Token for the current user has been added' do

      before :each do
        add_api_token
      end

      let :auth_info_response_without_token do
        plain_faraday_client.get('/my/user/me/auth-info')
      end

      let :auth_info_response_with_token_auth do
        plain_faraday_client.get('/my/user/me/auth-info') do |conn|
          conn.headers['authorization'] = "token #{@token_secret}"
        end
      end

      let :auth_info_response_with_token_as_basic_user do
        plain_faraday_client.get('/my/user/me/auth-info') do |conn|
          conn.basic_auth(@token_secret)
        end
      end

      scenario 'the token can be used to ' \
        ' authenticate via "authorization token" header' do

        # authentication w.o. token returns 401
        expect(plain_faraday_client.get('/my/user/me/auth-info').status).to be== 401

        expect(
          plain_faraday_client.get('/my/user/me/auth-info'){ |conn|
            conn.headers['authorization'] = "token #{@token_secret}"
          }.status).to be== 200
      end

      scenario 'the token can be used to ' \
        ' authenticate via basic auth as the username ' do
        http_client = Faraday.new(
          url: http_base_url,
          headers: { accept: 'application/json' }) do |conn|
            conn.adapter Faraday.default_adapter
            conn.response :json, content_type: /\bjson$/
            conn.basic_auth(@token_secret,'')
          end
          resp = http_client.get('/my/user/me/auth-info')
          expect(resp.status).to be== 200
      end

      scenario 'the token can be used to ' \
        ' authenticate via basic auth as the password' do
        http_client = Faraday.new(
          url: http_base_url,
          headers: { accept: 'application/json' }) do |conn|
            conn.adapter Faraday.default_adapter
            conn.response :json, content_type: /\bjson$/
            conn.basic_auth('',@token_secret)
          end
          resp = http_client.get('/my/user/me/auth-info')
          expect(resp.status).to be== 200
      end

      scenario 'editing the description of the token' do
        click_on_first 'Edit'
        fill_in 'Description', with: "The updated description"
        click_on 'Save'
        wait_until { first('.modal', text: 'OK') }
        wait_until do
          page.has_field?('Description', with: 'The updated description', disabled: true)
        end
      end

      scenario 'default scopes and editing the scope of the token' do

        expect(page).to have_field('read', checked: true, disabled: true)
        expect(page).to have_field('write', checked: true, disabled: true)

        expect(page).to have_field('admin read', checked: true, disabled: true)
        expect(page).to have_field('admin write', checked: true, disabled: true)

        expect(page).to have_field('system admin read', checked: false, disabled: true)
        expect(page).to have_field('system admin write', checked: false , disabled: true)


        # enable the system admin scopes

        click_on_first 'Edit'
        wait_until { all('.modal-body').empty? }
        check 'system admin read'
        check 'system admin write'
        click_on 'Save'
        wait_until { first('.modal', text: 'OK') }


        expect(page).to have_field('system admin read', checked: true, disabled: true)
        expect(page).to have_field('system admin write', checked: true, disabled: true)

        # make sure the front end doesen't show some fiction
        visit current_path
        expect(page).to have_field('system admin read', checked: true, disabled: true)
        expect(page).to have_field('system admin write', checked: true, disabled: true)
        db_token = ApiToken.where(token_part: @token_part).all.first
        expect(db_token[:scope_system_admin_read]).to be== true
        expect(db_token[:scope_system_admin_write]).to be== true

      end

      # RED ON CIDER, GREEN LOCALLY
      scenario 'an edited token such that it is expired can not be used to authenticate', pending: true do
        resp = plain_faraday_client.get('/my/user/me/auth-info') do |req|
          req.headers["Authorization"] = "Token #{@token_secret}"
        end
        expect(resp.status).to be== 200
        click_on_first 'Edit'
        wait_until { all('.modal-body').empty? }
        # capybara/webdriver sets Time values properly but events/notification seem not to work
        # we trigger those via arrow_up and then arrow_down again
        within(find("div.form-group", text: "Expires")) do
          fill_in 'Expires', with: (Time.now - 3.hours)
          find("input[type=datetime-local]").send_keys(:arrow_up)
          find("input[type=datetime-local]").send_keys(:arrow_down)
        end
        click_on 'Save'
        wait_until { page.has_field?('Expires', disabled: true) }
        resp2 = plain_faraday_client.get('/my/user/me/auth-info') do |req|
          req.headers["Authorization"] = "Token #{@token_secret}"
        end
        expect(resp2.status).to be== 401
      end

      scenario 'deleting a token works and makes it useless for authentication' do
        resp = plain_faraday_client.get('/my/user/me/auth-info') do |req|
          req.headers["Authorization"] = "Token #{@token_secret}"
        end
        expect(resp.status).to be== 200
        click_on_first 'Delete'
        wait_until { page.has_content? "Delete My API-Token #{@token_part}" }
        click_on 'Delete'
        wait_until { current_path =~ /^.*\/api-tokens\/$/ }
        resp2 = plain_faraday_client.get('/my/user/me/auth-info') do |req|
          req.headers["Authorization"] = "Token #{@token_secret}"
        end
        expect(resp2.status).to be== 401
      end

    end
  end


  context 'an admin, system-amdin and two users' do
    before :each do
      @admin = FactoryBot.create :admin
      @system_admin = FactoryBot.create :system_admin
      @user1 = FactoryBot.create :user
      @user2 = FactoryBot.create :user
    end


    scenario 'An user can not access/create an other users api TOKEN' do
      sign_in_as @user1
      visit "/my/user/#{@user2.id}/api-tokens/"
      wait_until{ page.has_content? 'ERROR 403'}
      click_on 'Dismiss'
      click_on 'Dismiss'
      click_on 'Add API-Token'
      fill_in 'Description', with: "Other users API-Token"
      click_on 'Add'
      expect(page).to have_content 'ERROR 403'
    end


    scenario 'An admin can not access/create an other users api TOKEN' do
      sign_in_as @admin
      visit "/my/user/#{@user2.id}/api-tokens/"
      expect(page).to have_content 'ERROR 403'
      click_on 'Dismiss'
      click_on 'Dismiss'
      click_on 'Add API-Token'
      fill_in 'Description', with: "Other users API-Token"
      click_on 'Add'
      expect(page).to have_content 'ERROR 403'
    end

    scenario 'A system-admin can create some users API-Token' do
      sign_in_as @system_admin
      visit "/my/user/#{@user2.id}/api-tokens/"
      click_on_first 'Add API-Token'
      fill_in 'Description', with: "My first token"
      click_on 'Add'
      wait_until{ page.has_content? "has been added"}
    end
  end
end
