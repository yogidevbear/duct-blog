(ns duct-blog.handler.authors
  (:require [ataraxy.response :as response]
            [buddy.hashers :as hashers]
            [clojure.java.jdbc :as jdbc]
            duct.database.sql
            [integrant.core :as ig]))

(defprotocol Authors
  (create-author [db email password firstName lastName]))

(extend-protocol Authors
  duct.database.sql.Boundary
  (create-author [{db :spec} email password firstName lastName]
    (let [pw-hash (hashers/derive password)
          results (jdbc/insert! db :authors {:email email, :password pw-hash, :firstName firstName, :lastName lastName})]
      (-> results ffirst val))))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [{[_ email password firstName lastName] :ataraxy/result}]
    (let [authorid (create-author db email password firstName lastName)]
      [::response/created (str "/authors/" authorid)])))
