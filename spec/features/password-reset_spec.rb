require 'spec_helper'

feature 'Password-Reset', type: :feature do

  context 'and admin and two users' do

    before :each do
      @admin = FactoryBot.create :admin
      @user1 = FactoryBot.create :user
      @user2 = FactoryBot.create :user
    end

    scenario 'A user can reset her password and sign in with it' do
      sign_in_as @user1

      visit '/my/user/me'
      click_on_first 'Password'

      fill_in 'password', with: "new password"
      click_on 'Set password'


      sign_out

      # user1 can not sign in with the old password anymore
      within('.navbar-leihs form') do
        fill_in 'user', with: @user1.email
        click_button
      end

      within('form.ui-form-signin') do
        fill_in 'password', with: @user1.password
        click_button
      end
      wait_until do
        page.has_content? 'Password authentication failed!'
      end


      # user1 can sign in with the new password 
      visit '/'
      within('.navbar-leihs form') do
        fill_in 'user', with: @user1.email
        click_button
      end

      within('form.ui-form-signin') do
        fill_in 'password', with: 'new password'
        click_button
      end
      click_on 'Auth-Info'
      expect(page).to have_content @user1.email
      
    end


    scenario 'A user not beeing an admin can not reset an other users password' do
      sign_in_as @user1
      visit "/my/user/#{@user2.id}/password"
      fill_in 'password', with: 'new password'
      click_on 'Set password'

      wait_until do
        page.has_content? '403' \
          and page.has_content? 'Forbidden!'
      end
    end


    scenario 'An admin can reset an other users password' do

      sign_in_as @admin
      visit "/my/user/#{@user2.id}/password"
      fill_in 'password', with: 'new password'
      click_on 'Set password'

      sign_out

      within('.navbar-leihs form') do
        fill_in 'user', with: @user2.email
        click_button
      end

      within('form.ui-form-signin') do
        fill_in 'password', with: 'new password'
        click_button
      end
      click_on 'Auth-Info'
      expect(page).to have_content @user2.email

    end

  end

end

