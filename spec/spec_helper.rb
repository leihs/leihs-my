require 'active_support/all'
require 'pry'

require 'config/database'
require 'config/factories'

require 'config/browser'
require 'config/http_client'

require 'helpers/global'
require 'helpers/user'

RSpec.configure do |config|

  config.include Helpers::Global
  config.include Helpers::User

  config.before :each do
    srand 1
  end

  config.after(:each) do |example|
    # auto-pry after failures, except in CI!
    unless (ENV['CIDER_CI_TRIAL_ID'].present? or ENV['NOPRY_ON_EXCEPTION'].present?)
      unless example.exception.nil?
        binding.pry if example.exception
      end
    end
  end
end
