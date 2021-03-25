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

  scenario 'Show message when no access to any pool' do
    err_msg = "You can not use the borrow section because you dont have access rights to any inventory pool!\nPlease contact your support or lending desk."
    # ensure there are no access rights:
    expect(AccessRight.where(user: @user).count).to be 0
    sign_in_as @user
    visit '/my/user/me'
    expect(page).to have_selector('.ui-no-access-rights-warning', text: err_msg)

    # create an access right and ensure the message is not show anymore:
    FactoryBot.create :access_right, user: @user
    expect(AccessRight.where(user: @user).count).to be 1
    visit '/my/user/me'
    expect(page).to have_no_selector('.ui-no-access-rights-warning', text: err_msg)
  end

  scenario 'Details of another user' do
    sign_in_as @admin
    visit "/my/user/#{@user.id}"
    expect(page).to have_content "User #{@user.firstname} #{@user.lastname}"
  end

end
