#!/usr/bin/env ruby

# NOTE: if git info already given via env vars, dont use git at all!
commit_id = ENV['BUILD_COMMIT_ID']
tree_id = ENV['BUILD_TREE_ID']
super_commit_id = ENV['BUILD_SUPER_COMMIT_ID']

require 'date'
require 'pathname'
require 'pry'
require 'socket'
require 'yaml'

__file__ = __FILE__
script = Pathname(__file__)

PROJECT_DIR = Pathname(File.expand_path(File.dirname(__FILE__))).join('..')

def present?(x)
  not x.nil? and not x == ""
end

unless present?(commit_id) && present?(tree_id)
  require 'git'
  project = Git.open(PROJECT_DIR)
  commit_id = project.log[0].sha
  head = project.object('HEAD')
  tree_id = head.gtree.objectish
end

unless present?(super_commit_id)
  require 'git'
  super_commit_id = begin
    super_project = Git.open(PROJECT_DIR.join('..'))
    super_project.log[0].sha
  rescue
    nil
  end
end

IO.write(PROJECT_DIR.join("resources").join("built-info.yml"),
         { 'commit_id' => commit_id,
           'hostname' => Socket.gethostname,
           'os' => Gem::Platform.local.to_s,
           'super_commit_id' => super_commit_id,
           'timestamp' => DateTime.now.iso8601,
           'tree_id' => tree_id }.to_yaml)
