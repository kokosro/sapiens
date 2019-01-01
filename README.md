# sapiens

A Clojure library designed to connect to kokos.one api

## Usage

`[org.clojars.kokos/sapiens "0.1.0"]`

```clojure
(ns example
 (:require [sapiens.core :as sapiens]))

(def conf {:base "https://api.kokos.one/v1"
          :email "..."
          :token "..."
          :secret "..."})

(sapiens/get-assets
conf {})


```

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
