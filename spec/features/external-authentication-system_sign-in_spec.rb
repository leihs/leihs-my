require 'spec_helper'
require 'pry'

feature 'Sign in via an external authentication system', type: :feature do

  before :each do
    database[:system_and_security_settings].update(
      external_base_url: http_base_url)

    @test_authentication_system = FactoryBot.create :authentication_system,
      id: 'test',
      name: 'Test Authentication-System',
      external_sign_in_url: "http://localhost:#{ENV['TEST_AUTH_SYSTEM_PORT']}/sign-in"

    @admin = FactoryBot.create :admin,
      email: 'admin@example.com',
      password: 'secret'

    database[:authentication_systems_users].insert user_id: @admin.id,
      authentication_system_id: @test_authentication_system.id

  end

  scenario 'sign in' do
    visit '/'
    within('.navbar-leihs form', match: :first) do
      fill_in 'user', with: 'admin@example.com'
      click_button
    end
    click_on @test_authentication_system.name
    click_on 'Yes, I am admin@example.com'
    click_on 'Auth-Info'
    expect(page).to have_content 'admin@example.com'
  end

  scenario 'fail to sign in' do
    visit '/'
    within('.navbar-leihs form', match: :first) do
      fill_in 'user', with: 'admin@example.com'
      click_button
    end
    click_on @test_authentication_system.name
    click_on 'No, I am not admin@example.com'
    wait_until do
      page.has_content? 'The user did not authenticate successfully!'
    end
  end

end
