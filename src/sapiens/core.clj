(ns sapiens.core
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [digest]))

(defn- create-nonce-generator
  [& [start-from]]
  (let [start-from (or start-from (System/currentTimeMillis))
        nonce-holder (atom start-from)]
    (fn []
      (locking nonce-holder 
        (reset! nonce-holder (inc @nonce-holder))))))

(def nonce-generator (create-nonce-generator))

(defn- sign
  [uri message nonce secret]
  (let [message (if (string? message) message
                    (json/write-str message))
        hm (digest/sha-256
            (str message nonce
                 (digest/sha-256 secret)))
        ]
    (digest/sha-512 (str uri hm))))

(defn- make-private-uri
  [config n a & [x]]
  (str (if x (:base config)
           "/v1")
       "/"
       (:token config)
       "/"
       (name n)
       "/"
       (name a)))


(defn- make-public-uri
  [config n a & [x]]
  (str (if x (:base config)
           "/v1")
       "/0/"
       (name n)
       "/"
       (name a)))

(defn- make-private-call
  [config n a data]
  (let [nonce (nonce-generator)
        data (assoc data :nonce nonce)
        data (if (or
                  (not (= (keyword n) :order))
                  (contains? data :email))
               data
                 (assoc data :email (:email config)))
        uri (make-private-uri config n a false)
        data (json/write-str data)
        signature (sign uri data nonce
                        (:secret config))
        r (http/request {:method "POST"
                         :url (make-private-uri
                               config n a true)
                         :content-type :json
                         :accept :json
                         :headers {"x-signature" signature}
                         :body data})]
    (json/read-str (:body r)
                   :key-fn #'keyword)))


(defn- make-public-call
  [config n a data]
  (let [nonce (nonce-generator)
        data (assoc data :nonce nonce)
        data (json/write-str data)
        uri (make-public-uri config n a false)
        r (http/request {:method "GET"
                         :url (make-public-uri
                               config n a true)
                         :content-type :json
                         :accept :json
                        
                         :body data})]
    (json/read-str (:body r)
                   :key-fn #'keyword)))

(defn get-assets
  [config data]
  (make-public-call
   config :public :assets data))

(defn get-pairs
  [config data]
  (make-public-call
   config :public :pairs data))

(defn get-ticker
  [config data]
  (make-public-call
   config :public :ticker data))

(defn get-limits
  [config data]
  (make-public-call
   config :public :limits data))

(defn initiate-order
  [config data]
  (make-private-call
   config :order :initiate data))

(defn cancel-order
  [config data]
  (make-private-call
   config :order :initiate data))

(defn info-order
  [config data]
  (make-private-call
   config :order :info data))

(defn get-open-orders
  [config data]
  (make-private-call
   config :auth :open-orders data))

(defn get-closed-orders
  [config data]
  (make-private-call
   config :auth :closed-orders data))

(defn get-cancelled-orders
  [config data]
  (make-private-call
   config :auth :cancelled-orders))

(defn get-expired-orders
  [config data]
  (make-private-call
   config :auth :expired-orders))

(defn get-depth
  [config data]
  (make-private-call
   config :book :depth data))
