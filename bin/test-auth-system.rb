#!/usr/bin/env ruby

require 'active_support/all'
require 'addressable/uri'
require 'haml'
require 'json'
require 'jwt'
require 'logger'
require 'pry'
require 'sinatra'


$logger = Logger.new($stdout)
$logger.level = Logger::INFO

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

EXT_SESSION_ID = 'ext_session_id_12345'


### Meta ######################################################################

get '/status' do
  $logger.info('/status')
  $logger.info({params: params})
  'OK'
end



### sign-in ###################################################################

get '/sign-in' do
  $logger.info('/sign-in')
  $logger.info({params: params})

  sign_in_request_token = params[:token]
  request_token_data = JWT.decode sign_in_request_token, ES256_pub_key, true, { algorithm: 'ES256' }
  email = request_token_data.first["email"]

  $logger.info({request_token_data: request_token_data})

  success_token = JWT.encode({
    sign_in_request_token: sign_in_request_token,
    email: email,
    success: true,
    external_session_id: EXT_SESSION_ID}, ES256_priv_key,'ES256')

  fail_token = JWT.encode({
    sign_in_request_token: sign_in_request_token,
    error_message: "The user did not authenticate successfully!"}, ES256_priv_key,'ES256')

  url = (request_token_data.first["server_base_url"] || 'http://localhost:3240') + request_token_data.first['path']

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


### sign-out ##################################################################

get '/sign-out' do
  sign_in_request_token = params[:token]
  # TODO do verify, catch and redirect back with error
  request_token_data = JWT.decode(sign_in_request_token, ES256_pub_key, true, { algorithm: 'ES256' }
                         ).first.with_indifferent_access
  $logger.info('/sign-out')
  $logger.info({params: params})
  $logger.info({request_token_data: request_token_data})

  html =
    Haml::Engine.new(
      <<-HAML.strip_heredoc
        %h1 SSO Sign-out To External Provider
        %p The real authentication-adapter will redirect this request to the SSO sign-out URL.
        %pre
          = #{request_token_data}
      HAML
    ).render

  html
end


### sso-external-sign-out #####################################################

get '/sso-sign-out' do
  $logger.info('/sso-sign-out')
  $logger.info({params: params})

  sid = params[:sid]

  sign_out_token = JWT.encode({
    external_session_id: sid}, ES256_priv_key,'ES256')

  url = "http://localhost:#{ENV['LEIHS_MY_HTTP_PORT'].presence ||'3240'}" + \
    "/sign-out/external-authentication/test/sso-sign-out"

  html =
    Haml::Engine.new(
      <<-HAML.strip_heredoc
        %h1 SSO Sign-out From External Provider
        %p
          %a{href: "#{url}?token=#{sign_out_token}"}
            %span
              Do sign out!
  HAML
    ).render

  html
end

