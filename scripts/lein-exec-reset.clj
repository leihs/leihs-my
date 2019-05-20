(require '[nrepl.core :as nrepl]) 

(with-open [conn (->> ".nrepl-port"
                      slurp
                      Integer.
                      (nrepl/connect :port))]
  (let [resp (-> (nrepl/client conn 1000)    ; message receive timeout required
                 (nrepl/message {:op "eval" :code "(app/reset)"})
                 nrepl/combine-responses)]
    (when-let [err (:err resp)]
      (print err)
      (.flush *out*)))) 

(System/exit 0)
