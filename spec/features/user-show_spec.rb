require 'spec_helper'
require 'pry'

feature 'User show', type: :feature do
  before :each do
    @user = FactoryBot.create :user
    @admin = FactoryBot.create :admin
  end

  scenario 'Details of my user' do
    sign_in_as @user
    visit '/my/user/me'
    expect(page).to have_content "My user"
    visit "/my/user/#{@user.id}"
    expect(page).to have_content "User #{@user.firstname} #{@user.lastname}"
  end

  scenario 'Details of another user' do
    sign_in_as @admin
    visit "/my/user/#{@user.id}"
    expect(page).to have_content "User #{@user.firstname} #{@user.lastname}"
  end

end
