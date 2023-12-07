require 'spec_helper'
require 'pry'

feature 'SSO Sign-in and out via an external authentication system', type: :feature do

  let :external_sso_sign_out_url do
    "http://localhost:#{ENV['TEST_AUTH_SYSTEM_PORT']}/sso-sign-out?sid=ext_session_id_12345"
  end

  let :leihs_base_url do
    "http://#{ENV['LEIHS_MY_HTTP_PORT'].presence ||'3240'}"
  end


  before :each do
    database[:system_and_security_settings].update(
      external_base_url: http_base_url)

    @test_authentication_system = FactoryBot.create :authentication_system,
      id: 'test',
      name: 'Test Authentication-System',
      external_sign_in_url: "http://localhost:#{ENV['TEST_AUTH_SYSTEM_PORT']}/sign-in",
      external_sign_out_url: "http://localhost:#{ENV['TEST_AUTH_SYSTEM_PORT']}/sign-out"


    @admin = FactoryBot.create :admin,
      email: 'admin@example.com',
      password: 'secret'

    database[:authentication_systems_users].insert user_id: @admin.id,
      authentication_system_id: @test_authentication_system.id

  end

  scenario 'SSO sign-in and sign-out to external' do
    visit '/'
    within('.navbar-leihs form', match: :first) do
      fill_in 'user', with: 'admin@example.com'
      click_button
    end
    click_on @test_authentication_system.name
    click_on 'Yes, I am admin@example.com'
    click_on 'Auth-Info'
    expect(page).to have_content 'admin@example.com'
    expect(page).to have_content 'ext_session_id_12345'
    find('.fa-user-circle').click
    click_on 'Logout'
    expect(page).to have_content "SSO Sign-out"
    expect(page).to have_content "The real authentication-adapter will redirect this request to the SSO sign-out URL"
  end


  scenario 'SSO sign-in and sign-out from external' do
    visit '/'
    within('.navbar-leihs form', match: :first) do
      fill_in 'user', with: 'admin@example.com'
      click_button
    end
    click_on @test_authentication_system.name
    click_on 'Yes, I am admin@example.com'
    click_on 'Auth-Info'
    expect(page).to have_content 'admin@example.com'
    expect(page).to have_content 'ext_session_id_12345'
    auth_info_url = current_url
    visit(external_sso_sign_out_url)
    click_on 'Do sign out'
    visit auth_info_url
    expect(page).to have_content 'Authentication required!'
    expect(page).not_to have_content 'admin@example.com'
    expect(page).not_to have_content 'ext_session_id_12345'
  end

end
