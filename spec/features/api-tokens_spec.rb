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
    end

    scenario 'creating an API-Token works' do
      add_api_token
      expect(page).to have_content @token_part
    end

    context 'an API-Token for the current user has been added' do

      before :each do
        add_api_token
      end

      let :auth_info_response_without_token do
        plain_faraday_json_client.get('/my/user/me/auth-info')
      end

      let :auth_info_response_with_token_auth do
        plain_faraday_json_client.get('/my/user/me/auth-info') do |conn|
          conn.headers['authorization'] = "token #{@token_secret}"
        end
      end

      let :auth_info_response_with_token_as_basic_user do
        plain_faraday_json_client.get('/my/user/me/auth-info') do |conn|
          conn.basic_auth(@token_secret)
        end
      end

      scenario 'the token can be used to ' \
        ' authenticate via "authorization token" header' do

        # authentication w.o. token returns 401
        expect(plain_faraday_json_client.get('/my/user/me/auth-info').status).to be== 401

        expect(
          plain_faraday_json_client.get('/my/user/me/auth-info'){|conn|
            conn.headers['authorization'] = "token #{@token_secret}"
          }.status).to be== 200
      end

      scenario 'the token can be used to ' \
        ' authenticate via basic auth as the username ' do
        plain_faraday_json_client.basic_auth(@token_secret,'')
        expect(plain_faraday_json_client.get('/my/user/me/auth-info').status).to be== 200
      end

      scenario 'the token can be used to ' \
        ' authenticate via basic auth as the password' do
        plain_faraday_json_client.basic_auth('', @token_secret)
        expect(plain_faraday_json_client.get('/my/user/me/auth-info').status).to be== 200
      end

      scenario 'editing the description of the token' do
        click_on_first @token_part
        click_on_first 'Edit'
        sleep 3
        fill_in 'Description', with: "The updated description"
        click_on 'Save'
        wait_until do
          page.has_field?('Description', with: 'The updated description', disabled: true)
        end
      end

      scenario 'an edited token such that it is expired can not be used to authenticate' do 
        plain_faraday_json_client.basic_auth(@token_secret,'')
        expect(plain_faraday_json_client.get('/my/user/me/auth-info').status).to be== 200
        click_on_first @token_part
        click_on_first 'Edit'
        sleep 3
        fill_in 'Expires', with: (Time.now - 3.hours).iso8601
        click_on 'Save'
        wait_until { page.has_field?('Expires', disabled: true) }
        expect(plain_faraday_json_client.get('/my/user/me/auth-info').status).to be== 401
      end

      scenario 'deleting a token works and makes it useless for authentication' do 
        plain_faraday_json_client.basic_auth(@token_secret,'')
        expect(plain_faraday_json_client.get('/my/user/me/auth-info').status).to be== 200
        click_on_first @token_part
        click_on_first 'Delete'
        wait_until { page.has_content? "Delete My API-Token #{@token_part}" }
        click_on 'Delete'
        wait_until { current_path =~ /^.*\/api-tokens\/$/ }
        expect(plain_faraday_json_client.get('/my/user/me/auth-info').status).to be== 401
      end

    end
  end
end
