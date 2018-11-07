module Helpers
  module User
    extend self

    def sign_in_as user
      visit '/'
      within('.navbar-leihs form') do
        fill_in 'user', with: user.email
        click_button
      end

      within('form.form-signin') do
        fill_in 'password', with: user.password
        click_button
      end

      expect(page).to have_content user.email
    end

  end
end
