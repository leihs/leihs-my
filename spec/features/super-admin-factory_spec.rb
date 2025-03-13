require "spec_helper"
require "pry"

feature "SuperAdmin" do
  scenario "can sign-in" do
    @admin = FactoryBot.create :system_admin,
      login: "admin",
      email: "admin@example.com",
      password: "secret"

    sign_in_as @admin
  end
end
