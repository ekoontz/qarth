(ns qarth.test
  (:gen-class)
  (require qarth.util qarth.impl.scribe clojure.string clojure.xml)
  (use qarth.oauth))

(def keysf (qarth.util/read-resource "keys.edn"))

; TODO way to build multi-services
; TODO tie service to requestor state somehow
(def service (build (assoc (:yahoo keysf) :type :scribe
                           :provider org.scribe.builder.api.YahooApi)))

(defn -main [& args]
  (let [sesh (request-session service)
        _ (println "Auth url:" (:url sesh))
        _ (print "Enter token: ")
        _ (flush)
        token (clojure.string/trim (read-line))
        sesh (verify-session service sesh token)
        ; TODO :url key?
        user-guid (->
                    (request-raw service sesh
                             {:url "https://social.yahooapis.com/v1/me/guid"})
                    :body
                    clojure.xml/parse
                    :content first :content first)
        _ (println "user-guid:" user-guid)
        user-info (-> (request-raw service sesh
                               {:url (str "https://social.yahooapis.com/v1/user/"
                                          user-guid "/profile")})
                    :body
                    clojure.xml/parse
                    :content)
        _ (println "user info:" (pr-str user-info))]))
