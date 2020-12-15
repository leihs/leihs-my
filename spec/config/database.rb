require 'addressable'
require 'sequel'

DB_ENV = ENV['LEIHS_DATABASE_URL'].presence

def http_uri
  @http_uri ||= \
    Addressable::URI.parse DB_ENV.gsub(/^jdbc:postgresql/,'http').gsub(/^postgres/,'http')
end

def database
  @database ||= \
    Sequel.connect(
      if DB_ENV
        # trick Addressable to parse db urls
        'postgres://' \
          + (http_uri.user.presence || ENV['PGUSER'].presence || 'postgres') \
          + ((pw = (http_uri.password.presence || ENV['PGPASSWORD'].presence)) ? ":#{pw}" : "") \
          + '@' + (http_uri.host.presence || ENV['PGHOST'].presence || ENV['PGHOSTADDR'].presence || 'localhost') \
          + ':' + (http_uri.port.presence || ENV['PGPORT'].presence || 5432).to_s \
          + '/' + ( http_uri.path.presence.try(:gsub,/^\//,'') || ENV['PGDATABASE'].presence || 'leihs') \
          + '?pool=5'
      else
        'postgresql://leihs:leihs@localhost:5432/leihs?pool=5'
      end
    )
end

def clean_db
  tables = database[ <<-SQL.strip_heredoc
    SELECT table_name
      FROM information_schema.tables
    WHERE table_type = 'BASE TABLE'
    AND table_schema = 'public'
    ORDER BY table_type, table_name;
    SQL
  ].map{|r| r[:table_name]}.reject { |tn| tn == 'schema_migrations' } \
    .join(', ').tap do |tables|
    database.run" TRUNCATE TABLE #{tables} CASCADE; "
  end
end

RSpec.configure do |config|
  config.before :each  do
    clean_db
    system("DATABASE_NAME=#{http_uri.basename} ./database/scripts/restore-seeds")
  end
  config.after :suite do
    clean_db
  end
end
