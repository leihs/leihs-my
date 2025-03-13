require "spec_helper"
require "pry"

describe "create user factory" do
  it "done" do
    FactoryBot.create(:user, password: "password")
  end
end
