class Group < Sequel::Model(:groups)
end

FactoryBot.define do
  factory :group do
    name { Faker::Name.last_name }
    description { Faker::Lorem.sentence }
  end
end
