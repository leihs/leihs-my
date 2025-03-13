require "spec_helper"
require "pry"

feature "Initial admin redirect", type: :feature do
  scenario "detects missing admin and redirects to admin app" do
    visit "/"

    # we get redirected to the initial admin because there are no admins yet
    expect(current_path).to eq "/admin/initial-admin"
    # ...which is a 404 -> see admin app for the actual feature
  end
end
