(ns playphraseme.views.login.view
  (:require [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [playphraseme.common.util :as util]
            [playphraseme.views.login.model :as model])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [re-frame-macros.core :as mcr :refer [let-sub]]))

(defn form-data []
  [@(rf/subscribe [::model/username])
   @(rf/subscribe [::model/password])])

(defn form-completed? []
  (->> (form-data) (remove string/blank?) count (= 2)))

(defn clear-error! []
  (rf/dispatch [::model/error-message nil]))

(defn on-login [e]
  (-> e .preventDefault)
  (clear-error!)
  (when (form-completed?)
    (let [[username password] (form-data)]
      (go
        #_(let [res (<! (auth username password))]
          (if (success? res)
            (do
              (if @(rf/subscribe [:metrics-need-save])
                (rf/dispatch [:save-metrics])
                (rf/dispatch [:load-metrics]))
              (util/go-url! "/#/"))
            (rf/dispatch [::model/error-message (-> res :body :error)]))))))
  false)

(defn page []
  [:form {:on-submit on-login}
   [:div.fa-button
    [:a.fa.fa-facebook {:href "/api/v1/auth/facebook"}]
     [:span "Sign in with Facebook"]]
   [:h1 "Or sign in with email"]
   [:div
    (when-let [error-message @(rf/subscribe [::model/error-message])]
      [:div.alert.alert-danger
       {:role "alert"}
       error-message])
    [:div.d-flex
     [:input.input {:type        "email" :id "input-email"
                    :placeholder "Email Address"
                    :value       (-> (form-data) first)
                    :on-change   (fn [e]
                                   (clear-error!)
                                   (rf/dispatch [::model/username (-> e .-target .-value)]))
                    :auto-focus  true}]]]
   [:div.d-flex
    [:input.input {:type        "password"
                   :id          "input-password"
                   :value       (-> (form-data) second)
                   :placeholder "Password"
                   :on-change   (fn [e]
                                  (clear-error!)
                                  (rf/dispatch [::model/password (-> e .-target .-value)]))}]]
   [:div.d-flex
    [:div.grow]
    [:button.form-button {:type "submit" :disabled (not (form-completed?))} "SIGN IN"]
    [:div.grow]]

   [:div.page-footer-links
    [:p.text-center
     [:a {:href "/#/reset-password"} "Forgot Password?"]
     " New to us? " [:a {:href "/#/register"} "Register."]]]])

