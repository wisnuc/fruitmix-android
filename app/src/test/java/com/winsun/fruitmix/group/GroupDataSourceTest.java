package com.winsun.fruitmix.group;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.FakeGroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/20.
 */

public class GroupDataSourceTest {

    private GroupDataSource groupDataSource;

    @Before
    public void setup() {

        groupDataSource = FakeGroupDataSource.getInstance();
        groupDataSource.clearGroups();
    }

    private String testGroupUuid = "testGroupUuid";

    private String testStationId = "testStationId";

    private String testGroupName1 = "testGroupName1";

    @Test
    public void testAddGroup() {

        PrivateGroup privateGroup = new PrivateGroup(testGroupUuid, testGroupName1,"testOwnerGUID",testStationId, Collections.<User>emptyList());

        groupDataSource.addGroup(privateGroup, new BaseOperateCallback() {
            @Override
            public void onSucceed() {

            }

            @Override
            public void onFail(OperationResult operationResult) {

            }
        });

        groupDataSource.getAllGroups(new BaseLoadDataCallback<PrivateGroup>() {
            @Override
            public void onSucceed(List<PrivateGroup> data, OperationResult operationResult) {

                assertEquals(1, data.size());

                assertEquals(testGroupUuid, data.get(0).getUUID());
                assertEquals(testGroupName1, data.get(0).getName());

            }

            @Override
            public void onFail(OperationResult operationResult) {

            }
        });



    }

    @Test
    public void testGetGroupByUUID() {

        testAddGroup();

        groupDataSource.getAllUserCommentByGroupUUID(new GroupRequestParam(testGroupUuid,testStationId), new BaseLoadDataCallback<UserComment>() {
            @Override
            public void onSucceed(List<UserComment> data, OperationResult operationResult) {


            }

            @Override
            public void onFail(OperationResult operationResult) {

            }
        });

    }

    @Test
    public void testAddTextComment() {

        long time = System.currentTimeMillis();

        final String testText = "testAddTextComment";

        testAddGroup();

        UserComment userComment = new TextComment(Util.createLocalUUid(),new User(), time, testGroupUuid,testStationId,testText);

        groupDataSource.insertUserComment(new GroupRequestParam(testGroupUuid,testStationId), userComment, new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                groupDataSource.getAllUserCommentByGroupUUID(new GroupRequestParam(testGroupUuid,testStationId), new BaseLoadDataCallback<UserComment>() {
                    @Override
                    public void onSucceed(List<UserComment> data, OperationResult operationResult) {

                        assertEquals(1, data.size());

                        TextComment result = (TextComment) data.get(0);

                        assertEquals(testText, result.getText());

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                    }
                });


            }

            @Override
            public void onFail(OperationResult operationResult) {

            }
        });

    }


}
