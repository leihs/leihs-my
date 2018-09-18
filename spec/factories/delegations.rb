class Delegation < Sequel::Model(:users)
end

FactoryBot.define do
  factory :delegation do
    firstname { Faker::Name.last_name }
    delegator_user_id { User.all.sample.id }
  end
end
