require 'spec_helper'
require 'pry'

feature 'Sign in via an external authentication system', type: :feature do

  before :each do
    database[:settings].insert_conflict.insert({})
    database[:settings].update(
      external_base_url: ENV['LEIHS_MY_HTTP_BASE_URL'])
    
    @test_authentication_system = FactoryBot.create :authentication_system, 
      id: 'test', 
      name: 'Test Authentication-System', 
      external_url: "http://localhost:#{ENV['TEST_AUTH_SYSTEM_PORT']}/sign-in"

    @admin = FactoryBot.create :admin, 
      email: 'admin@example.com',
      password: 'secret'

    database[:authentication_systems_users].insert user_id: @admin.id,
      authentication_system_id: @test_authentication_system.id

  end

  scenario 'sign in' do
    visit '/'
    fill_in 'email', with: 'admin@example.com'
    click_on 'Continue'
    click_on 'Continue'
    click_on 'Yes, I am admin@example.com'
    wait_until do
      page.has_content? "Sign out"
    end
  end

  scenario 'fail to sign in' do
    visit '/'
    fill_in 'email', with: 'admin@example.com'
    click_on 'Continue'
    click_on 'Continue'
    click_on 'No, I am not admin@example.com'
    wait_until do
      page.has_content? 'Authentication failed!'
    end
  end

end
