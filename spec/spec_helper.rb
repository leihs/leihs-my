require "active_support/all"
require "pry"

require_relative "../database/spec/config/database"
require "config/factories"

require "config/browser"
require "config/http_client"

require "helpers/global"
require "helpers/user"

RSpec.configure do |config|
  config.include Helpers::Global
  config.include Helpers::User

  config.before :each do
    srand 1
    db_clean
    db_restore_data seeds_sql
  end

  config.after(:each) do |example|
    # auto-pry after failures, except in CI!
    if !ENV["CIDER_CI_TRIAL_ID"].present? && ENV["PRY_ON_EXCEPTION"].present?
      unless example.exception.nil?
        binding.pry if example.exception # standard:disable Lint/Debugger
      end
    end
  end
end
