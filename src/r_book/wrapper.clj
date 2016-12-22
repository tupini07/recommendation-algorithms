;; Taken from https://github.com/cgroza/goodreads-clj
;; and modified a bit

(ns r-book.wrapper
   (:require [clj-http.client :as http]
             [clojure.string :as string]
             [clojure.xml :as xml]
             [clojure.zip :as zip]
             [clojure.data.json :as json]
             [oauth.client :as oauth])
   (:import [java.net URLEncoder]))

;; Developers authentication
(def dev-key "6R18lb4Kdymg0f2bmAAckg")
(def secret "QzL28tUoXtRSoFcMiaNleBoP7WfPMw8nQU77STWdETg")

;; Goodreads API URLs
(def root-url "https://www.goodreads.com")

(def url-review-title "/book/title.xml")
(def url-review-stats-isbns "/book/review_counts.json")
(def url-id-of-isbns "/book/isbn_to_id")
(def url-info-author-id "/author/show.xml")
(def url-author-id-name "/api/author_url")
(def url-author-books-page "/author/list.xml")
(def url-events-in-area "/event/index.xml")
(def url-find-group "/group/search.xml")
(def url-info-group-id "/group/show/")
(def url-listopia-book-id "/list/book/")
(def url-user-info "/user/show/")
(def url-user-read-status-id "/read_statuses/")
(def url-user-status "/user_status/show/")
(def url-user-reviews "/review/list")
(def url-recent-user-statuses "/user_status/index.xml")
(def url-recent-member-reviews "/review/recent_reviews.xml")
(def url-review "/review/show.xml")
(def url-user-review-book "/review/show_by_user_and_book.xml")
(def url-find-book "/search/index.xml")
(def url-series "/series/show/")
(def url-series-author "/series/list")
(def url-series-work "/work/")

(def url-request-token  (format "%s/oauth/request_token/" root-url))
(def url-authorize  (format "%s/oauth/authorize/" root-url))
(def url-access-token (format "%s/oauth/access_token/" root-url))

;; Default parameters
(def default-data {"key" dev-key})

(defn parametric [data]
  "Formats parameters into URLs."
  (string/join "&" (map (fn [[k v]] (str k "=" (. URLEncoder encode v))) data)))

(defn url-get
  "Retrieves the xml response from api-url"
  ([api-url] (url-get api-url {}))
  ([api-url data]
   (http/get (str root-url api-url "?" (parametric (conj default-data data))))))


(defn parse-xml [xxml]
  "Returns zipped xml content."
  (->  xxml
       (.getBytes "UTF-8")
       java.io.ByteArrayInputStream.
       xml/parse
       zip/xml-zip))


(defn parse-xml-body [response]
  "Returns zipped xml response."
  (->> response
       :body
       parse-xml))

(def url-get-parsed (comp parse-xml-body url-get))

(defn id-author-name [author]
  (url-get-parsed (str url-author-id-name "/" author) {}))

(defn id-of-isbns [isbns]
  (->
   (url-get url-id-of-isbns {"isbn" (string/join "," isbns)})
   :body
   (string/split  #",")))

(defn info-author-id [id]
  (url-get-parsed url-info-author-id {"id" id}))

(defn author-books-page
  ([id] (author-books-page id 1))
  ([id page]
   (url-get-parsed url-author-books-page {"id" id "page" (str page)})))

(defn reviews-widget-title [title author & rating]
  (let [data {"title" title "author" author}]
    (if (not-empty rating) (assoc data "rating" (str (first rating))) data))
  (url-get-parsed url-review-title))

(defn review-stats-isbns[isbns]
  (->> {"isbns" (string/join "," isbns)}
       (url-get-parsed url-review-stats-isbns)
       :body
       json/read-str))

(defn events-in-area [latitude longitude country-code postal-code]
  (url-get-parsed url-events-in-area {"latitude" latitude
                                      "longitude" longitude
                                      "search[country_code]" country-code
                                      "search[postal_code]" postal-code}))

(defn find-group
  ([name] (find-group name 1))
  ([name page]
   (url-get-parsed url-find-group {"q" name "page" (str page)})))

(defn info-group-id [id sort order]
  ;; sort: Field to sort topics by. One of 'comments_count', 'title', 'updated_at', 'views'
  ;; order: 'a' for ascending, 'd' for descending
  (url-get-parsed (str url-info-group-id id ".xml") {"sort" sort "order" order}))

(defn listopia-book-id [id]
  (url-get-parsed (str url-listopia-book-id id ".xml")))

(defn user-read-status [id]
  (url-get-parsed (str url-user-read-status-id id) {"format" "xml"}))

(defn user-status [id]
  (url-get-parsed (str url-user-status id) {"format" "xml"}))

(defn recent-user-statuses []
  (url-get-parsed url-recent-user-statuses {}))

(defn recent-member-reviews []
  (url-get-parsed url-recent-member-reviews))

(defn review
  ([id] (review id 1))
  ([id page] (url-get-parsed url-review {"id" id "page" (str page)})))

(defn user-review-book
  ([user-id book-id] (user-review-book user-id book-id nil))
  ([user-id book-id include-review-on-work]
   (url-get-parsed url-user-review-book {"user_id" user-id "book_id" book-id
                                         "include_review_on_work"
                                         (if include-review-on-work
                                           "true" "false")})))

(defn user-info [id]
  (-> (url-get (str url-user-info id ".xml") {})
      :body
      (string/split #"\n")
      rest
      (string/join)
      parse-xml))

(defn user-reviews [id]
  (url-get-parsed url-user-reviews {"v" "2"
                                    "id" id
                                    "shelf" "read"
                                    "sort" "rating"
                                    "per_page" "200"}))

(defn find-book
  ([query-string] (find-book query-string 1))
  ([query-string page] (find-book query-string page "all"))
  ([query-string page field]
   (url-get-parsed url-find-book
                   {"q" query-string
                    "page" (str page)
                    "field" field})))

(defn series [id]
  (url-get-parsed (str url-series id) {"format" "xml"}))

(defn series-author [author-id]
  (url-get-parsed url-series-author {"format" "xml" "id" author-id}))

(defn series-work [work-id]
  (url-get-parsed (str url-series-work work-id "/series") {"format" "xml"}))


;; Good stuff  --------------------------------------------------------------------

(defn get-tag-content [tag]
  "small helper function that return the first content of a tag."
  (let [cntt (:content tag)]
    (if (= (count cntt) 1)
      (first cntt)
      cntt)))

(defn get-tag [xml tag]
  (-> (first (filter #(= (:tag %) tag) xml))
      get-tag-content))

(defn get-user-books [uid]
  (let [reviews (-> (user-reviews uid)
                    first
                    :content
                    second)]
    (->> reviews
         get-tag-content
         (map #(let [{rc :content} %
                     book-el (get-tag rc :book)
                     rat-el (get-tag rc :rating)]
                 
                 {:book-id (-> book-el
                               (get-tag :id))
                  
                  :isbn    (-> book-el
                               (get-tag :isbn))
                  
                  :isbn13  (-> book-el
                               (get-tag :isbn13))
                  
                  :title   (-> book-el
                               (get-tag :title))
                  
                  :rating  (-> rat-el
                               read-string)}))))) ;make an int
