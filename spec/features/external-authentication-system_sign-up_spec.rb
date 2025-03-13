require "spec_helper"
require "pry"

feature "Sign in via an external authentication system", type: :feature do
  before :each do
    database[:system_and_security_settings].update(
      external_base_url: http_base_url
    )

    @test_authentication_system = FactoryBot.create :authentication_system,
      id: "test",
      name: "Test Authentication-System",
      external_sign_in_url: "http://localhost:#{ENV["TEST_AUTH_SYSTEM_PORT"]}/sign-in",
      sign_up_email_match: ".*@example.com"

    @admin = FactoryBot.create :admin,
      email: "admin@example.com",
      password: "secret"

    database[:authentication_systems_users].insert user_id: @admin.id,
      authentication_system_id: @test_authentication_system.id
  end

  scenario "sign up" do
    visit "/"
    within(".navbar-leihs form", match: :first) do
      fill_in "user", with: "unknown@example.com"
      click_button
    end

    click_on @test_authentication_system.name
    wait_until { page.has_content? "Yes, I am unknown@example.com" }

    # the adapter would now create or update the user, we simulate this
    # here for simplicity in this process
    @sign_up_user = FactoryBot.create :user, email: "unknown@example.com"

    database[:authentication_systems_users].insert user_id: @sign_up_user.id,
      authentication_system_id: @test_authentication_system.id

    click_on "Yes, I am unknown@example.com"
    visit "/my/auth-info"
    expect(page).to have_content "unknown@example.com"
  end
end
