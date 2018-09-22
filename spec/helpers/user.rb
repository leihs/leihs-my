module Helpers
  module User
    extend self

    def sign_in_as user
      visit '/'
      fill_in 'email', with: user.email
      click_on 'Continue'
      fill_in 'password', with: user.password
      click_on 'Sign in'
      expect(page).to have_content user.email
    end

  end
end
