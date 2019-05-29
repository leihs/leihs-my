require 'spec_helper'

feature 'Password, sign-in, sign-out and session ' do
  context 'an existing user' do

    before :each do
      @admin = FactoryBot.create :admin
      @user = FactoryBot.create :user
    end

    let :user do
      @user
    end

    scenario 'signing in with the proper password works' do
      visit '/'
      fill_in 'user', with: user.email
      click_on 'Login'
      fill_in 'password', with: user.password
      click_on 'Weiter'
      find('.fa-user-circle').click
      expect(page).to have_content user.lastname
    end

    scenario 'signing in with wrong password does not work ' do
      visit '/'
      fill_in 'user', with: user.email
      click_on 'Login'
      fill_in 'password', with: 'bogus'
      click_on 'Weiter'
      expect(page).to have_content 'Falsches Passwort!'
    end

    scenario 'signing out after signing in with passwort works' do
      visit '/'
      fill_in 'user', with: user.email
      click_on 'Login'
      fill_in 'password', with: user.password
      click_on 'Weiter'
      find('.fa-user-circle').click
      expect(page).to have_content user.lastname
      click_on 'Logout'
      expect(page).to have_content 'Login'
    end

  end
end
