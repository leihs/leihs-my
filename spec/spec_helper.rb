require 'active_support/all'
require 'pry'

require 'config/database'
require 'config/factories'
require 'config/web'
require 'helpers/global'
require 'helpers/user'

RSpec.configure do |config|

  config.include Helpers::Global
  config.include Helpers::User

  config.before :each do
    srand 1
  end

end
