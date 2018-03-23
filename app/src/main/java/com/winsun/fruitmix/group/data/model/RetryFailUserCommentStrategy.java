package com.winsun.fruitmix.group.data.model;

/**
 * Created by Administrator on 2018/3/23.
 */

public interface RetryFailUserCommentStrategy {

    void handleRetryFailUserComment(UserComment failUserCommentInDraft);

}
