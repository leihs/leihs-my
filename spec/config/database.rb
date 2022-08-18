require 'sequel'

require 'logger'

def db_name
  ENV['LEIHS_DATABASE_NAME'] || ENV['DB_NAME'] || 'leihs'
end

def db_port
  Integer(ENV['DB_PORT'].presence || ENV['PGPORT'].presence || 5432)
end

def db_con_str
  logger = Logger.new(STDOUT)
  s = 'postgres://' \
    + (ENV['PGUSER'].presence || 'postgres') \
    + ((pw = (ENV['DB_PASSWORD'].presence || ENV['PGPASSWORD'].presence)) ? ":#{pw}" : "") \
    + '@' + (ENV['PGHOST'].presence || 'localhost') \
    + ':' + (db_port).to_s \
    + '/' + (db_name)
  logger.info "SEQUEL CONN #{s}"
  s
end

def database
  @database ||= Sequel.connect(db_con_str)
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
    system("LEIHS_DATABASE_NAME=#{db_name} ./database/scripts/restore-seeds")
  end
  config.after :suite do
    clean_db
  end
end
