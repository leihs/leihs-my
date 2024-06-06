(ns leihs.my.auth-info.back)

(defn auth-info-handler [request]
  (when-let [auth-ent (:authenticated-entity request)]
    {:status 200
     :body auth-ent}))
