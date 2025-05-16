package com.example.aviaappmobile;



class PaymentCard {
    private int CardId;
    private String CardNumber;
    private String GoodThroghDate;
    private Integer CVC; // Может быть null

    // Геттеры и сеттеры
    public int getCardId() {
        return CardId;
    }

    public void setCardId(int cardId) {
        this.CardId = cardId;
    }

    public String getCardNumber() {
        return CardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.CardNumber = cardNumber;
    }

    public String getGoodThroghDate() {
        return GoodThroghDate;
    }

    public void setGoodThroghDate(String goodThroghDate) {
        this.GoodThroghDate = goodThroghDate;
    }

    public Integer getCVC() {
        return CVC;
    }

    public void setCVC(Integer CVC) {
        this.CVC = CVC;
    }
}