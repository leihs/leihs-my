require "faraday"
require "faraday_middleware"

def plain_faraday_client
  Faraday.new(
    url: http_base_url,
    headers: {accept: "application/json"}
  ) do |conn|
    conn.adapter Faraday.default_adapter
    conn.response :json, content_type: /\bjson$/
  end
end
