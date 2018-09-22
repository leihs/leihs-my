require 'config/database.rb'
require 'factory_bot'
require 'faker'

Sequel::Model.db = database
Sequel::Model.send :alias_method, :save!, :save

RSpec.configure do |config|
  config.include FactoryBot::Syntax::Methods

  config.before(:suite) do
    FactoryBot.definition_file_paths = \
      %w{./spec/factories ./leihs-clj-shared/factories}
    FactoryBot.find_definitions
  end
end
