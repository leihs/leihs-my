(ns leihs.shared
  (:require [clojure.java.jdbc :as jdbc]
            [leihs.core.sql :as sql]))

(defn password-hash
  [password tx]
  (->> [(sql/call :crypt
                  (sql/call :cast password :text)
                  (sql/raw "gen_salt('bf', 10)"))
        :pw_hash]
       sql/select
       sql/format
       (jdbc/query tx)
       first
       :pw_hash))
