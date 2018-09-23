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

      click_on_first @user1.email
      click_on_first 'Password'

      fill_in 'password', with: "new password"
      click_on 'Set password'


      click_on 'Sign out'

      # user1 can not sign in with the old password anymore
      fill_in 'email', with: @user1.email
      click_on 'Continue'
      fill_in 'password', with: @user1.password
      click_on 'Sign in'
      wait_until do
        page.has_content? 'Password authentication failed!'
      end


      # user1 can not sign in with the new password 
      visit '/'
      fill_in 'email', with: @user1.email
      click_on 'Continue'
      fill_in 'password', with: 'new password'
      click_on 'Sign in'
      expect(page).to have_content @user1.email
      
    end


    scenario 'A user not beeing an admin can not reset an other users password' do
      sign_in_as @user1
      visit "/user/#{@user2.id}/password"
      fill_in 'password', with: 'new password'
      click_on 'Set password'

      wait_until do
        page.has_content? '403' \
          and page.has_content? 'Forbidden!'
      end
    end


    scenario 'An admin can reset an other users password' do

      sign_in_as @admin
      visit "/user/#{@user2.id}/password"
      fill_in 'password', with: 'new password'
      click_on 'Set password'

      click_on 'Sign out'

      fill_in 'email', with: @user2.email
      click_on 'Continue'
      fill_in 'password', with: 'new password'
      click_on 'Sign in'
      expect(page).to have_content @user2.email

    end

  end

end

