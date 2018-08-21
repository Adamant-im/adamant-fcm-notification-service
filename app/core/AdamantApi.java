package core;

import core.entities.UnnormalizedTransactionMessage;
import core.entities.transaction_assets.TransactionChatAsset;
import core.entities.transaction_assets.TransactionStateAsset;
import core.requests.NewAccount;
import core.requests.ProcessTransaction;
import core.responses.*;
import io.reactivex.Flowable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.math.BigInteger;

public interface AdamantApi {
    long BASE_TIMESTAMP = 1504371600000L; //2017-08-02 17:00:00
    int SYNCHRONIZE_DELAY_SECONDS = 6;
    int MAX_TRANSACTIONS_PER_REQUEST = 100;
    String ORDER_BY_TIMESTAMP_DESC = "timestamp:desc";
    String ORDER_BY_TIMESTAMP_ASC = "timestamp:asc";


    @GET("chats/get")
    Flowable<TransactionList<TransactionChatAsset>> getMessageTransactions(
            @Query("fromHeight") long height,
            @Query("orderBy") String order
    );

    @GET("chats/get")
    Flowable<TransactionList<TransactionChatAsset>> getMessageTransactions(
            @Query("orderBy") String order,
            @Query("offset") long offset
    );

    @GET("accounts/getPublicKey")
    Flowable<PublicKeyResponse> getPublicKey(@Query("address") String address);

    @GET("blocks/getHeight")
    Flowable<BlockHeight> getHeight();

}
