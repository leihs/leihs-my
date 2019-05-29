require 'spec_helper'

feature 'Password, sign-in, sign-out and session ' do

    let :user do
      @user
    end

    context 'a user with password ' do

      before :each do
        @admin = FactoryBot.create :admin
        @user = FactoryBot.create :user
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

      scenario 'the session dies when account_enabled is set to false' do
        visit '/'
        fill_in 'user', with: user.email
        click_on 'Login'
        fill_in 'password', with: user.password
        click_on 'Weiter'
        find('.fa-user-circle').click
        expect(page).to have_content user.lastname
        database[:users].where(id: user.id).update(account_enabled: false)
        # indirect proofe that we are not logged in anymore
        visit '/'
        expect(page).to have_content 'Login'
      end


      scenario 'the session is still valid when password_sign_in_enabled is set to false' do
        visit '/'
        fill_in 'user', with: user.email
        click_on 'Login'
        fill_in 'password', with: user.password
        click_on 'Weiter'
        find('.fa-user-circle').click
        expect(page).to have_content user.lastname
        database[:users].where(id: user.id).update(password_sign_in_enabled: false)
        # indirect proofe that we are still signed in
        visit '/'
        find('.fa-user-circle').click
        expect(page).to have_content user.lastname
      end

    end


    context 'a user with where password_sign_in_enabled is false' do

      before :each do
        @admin = FactoryBot.create :admin
        @user = FactoryBot.create :user, password_sign_in_enabled: false
      end

      scenario 'signing in with the proper password does not work' do
        visit '/'
        fill_in 'user', with: user.email
        click_on 'Login'
        expect(page).to have_content 'Anmelden ist mit diesem Benutzerkonto nicht möglich'
      end

    end


    context 'a user with where account_enabled is false' do

      before :each do
        @admin = FactoryBot.create :admin
        @user = FactoryBot.create :user, account_enabled: false
      end

      scenario 'signing in with the proper password does not work' do
        visit '/'
        fill_in 'user', with: user.email
        click_on 'Login'
        expect(page).to have_content 'Anmelden ist mit diesem Benutzerkonto nicht möglich'
      end

    end

end
