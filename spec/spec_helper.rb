require 'active_support/all'
require 'pry'

require 'config/database'
require 'config/factories'

def base_url
  @base_url ||= ENV['LEIHS_MY_HTTP_BASE_URL'].presence || 'http://localhost:3240'
end

def port
  @port ||= Addressable::URI.parse(base_url).port
end

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

end
