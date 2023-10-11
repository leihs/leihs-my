(ns leihs.my.manuel.logging
  (:require [taoensso.timbre :as log])
  )

;; REPL-COMMANDS

;; simple test: default
; (require '[taoensso.timbre :as log])
; (log/warn "foo")
; (log/set-level! :debug)
;
; *ns*
; (ns leihs.my.manuel.logging)
; (log/set-level! :debug)
; (require '[taoensso.timbre :as log])
;
; (test-logging)    ;OR
; (-main)
;
; (log/warn "foo")

(defn test-logging "Provided logging-types by taoensso.timbre" []
  (log/debug "test-debug")
  (log/error "test-error")
  (log/info "test-info")
  (log/spy "test-spy")
  (log/warn "test-warn")
  )

(defn -main [& _]
  (println "call 'test-logging' by -main")
  (test-logging)
  )