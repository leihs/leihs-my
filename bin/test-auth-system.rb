require 'active_support/all'
require 'addressable/uri'
require 'haml'
require 'json'
require 'jwt'
require 'pry'
require 'sinatra'

# for now: 
# start with `bundle exec ruby ... -p PORT`

priv_key = <<-KEY.strip_heredoc
  -----BEGIN EC PRIVATE KEY-----
  MHcCAQEEIHErTjw8Z1yNisngEuZ5UvBn1qM2goN3Wd1V4Pn3xQeYoAoGCCqGSM49
  AwEHoUQDQgAEzGT0FBI/bvn21TOuLmkzDwzRsIuOyIf9APV7DAZr3fgCqG1wzXce
  MGG42wJIDRduJ9gb3LJiewqzq6VVURvyKQ==
  -----END EC PRIVATE KEY-----
KEY

pub_key = <<-KEY.strip_heredoc
  -----BEGIN PUBLIC KEY-----
  MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEzGT0FBI/bvn21TOuLmkzDwzRsIuO
  yIf9APV7DAZr3fgCqG1wzXceMGG42wJIDRduJ9gb3LJiewqzq6VVURvyKQ==
  -----END PUBLIC KEY-----
KEY

ES256_priv_key = OpenSSL::PKey.read priv_key
ES256_pub_key = OpenSSL::PKey.read pub_key


### intialize #################################################################

def initialize
  super()
  @hooks = []
end


### Meta ######################################################################

get '/status' do
  'OK'
end



### sign-in ###################################################################

get '/sign-in' do
  sign_in_request_token = params[:token]
  # TODO do verify, catch and redirect back with error 
  token_data = JWT.decode sign_in_request_token, ES256_pub_key, true, { algorithm: 'ES256' }
  email = token_data.first["email"]

  success_token = JWT.encode({
    sign_in_request_token: sign_in_request_token,
    email: email,
    success: true}, ES256_priv_key,'ES256')

  fail_token = JWT.encode({
    sign_in_request_token: sign_in_request_token,
    error_message: "The user did not authenticate successfully!"}, ES256_priv_key,'ES256')

  url = (token_data.first["server_base_url"] || 'http://localhost:3220') + token_data.first['path'] 


  html = 
    Haml::Engine.new(
      <<-HAML.strip_heredoc
        %h1 The Super Secure Test Authentication System

        %p 
          Answer truthfully! Are you 
          %em 
            #{email}
          ?
        %ul
          %li
            %a{href: "#{url}?token=#{success_token}"}
              %span
                Yes, I am 
                %em
                  #{email}
          %li
            %a{href: "#{url}?token=#{fail_token}"}
              %span 
                No, I am not 
                %em
                  #{email}
      HAML
    ).render

  html
end

