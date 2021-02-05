FactoryBot.modify do
  factory :group do
    name { Faker::Name.last_name }
    description { Faker::Lorem.sentence }
  end
end
