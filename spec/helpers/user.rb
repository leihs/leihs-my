module Helpers
  module User
    extend self

    def sign_in_as user
      visit '/'
      within('.navbar-leihs form', match: :first) do
        fill_in 'user', with: user.email
        click_button
      end

      within('form.ui-form-signin') do
        fill_in 'password', with: user.password
        click_button
      end

      visit '/my/auth-info'
      expect(page).to have_content user.email
    end

    def sign_out
      within 'nav.navbar-leihs' do
        find('.fa-user-circle').click
        within '.dropdown-menu.show' do
          click_on 'Logout'
        end
      end
    end

  end
end
