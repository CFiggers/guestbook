(ns guestbook.db.core-test
  (:require
   [guestbook.db.core :refer [*db*] :as db]
   [java-time.pre-java8]
   [luminus-migrations.core :as migrations]
   [clojure.test :as test]
   [next.jdbc :as jdbc]
   [guestbook.config :refer [env]]
   [mount.core :as mount]))

(test/use-fixtures
  :once
  (fn [f]
    (mount/start
     #'guestbook.config/env
     #'guestbook.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(test/deftest test-messages
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (test/is (= 1 (db/save-message!
              t-conn
              {:name "Bob"
               :message "Hello, World"}
              {:connection t-conn})))
    (test/is (= {:name "Bob"
            :message "Hello, World"}
           (-> (db/get-messages t-conn {})
               (first)
               (select-keys [:name :message]))))))
