package com.winsun.fruitmix.group;

import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.FakeGroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupDataSource;
import com.winsun.fruitmix.user.User;

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
    private String testGroupName1 = "testGroupName1";

    @Test
    public void testAddGroup() {

        PrivateGroup privateGroup = new PrivateGroup(testGroupUuid, testGroupName1, Collections.<User>emptyList());

        groupDataSource.addGroup(Collections.singleton(privateGroup));

        List<PrivateGroup> data = groupDataSource.getAllGroups();

        assertEquals(1, data.size());

        assertEquals(testGroupUuid, data.get(0).getUUID());
        assertEquals(testGroupName1, data.get(0).getName());

    }

    @Test
    public void testGetGroupByUUID() {

        testAddGroup();

        PrivateGroup group = groupDataSource.getGroupByUUID(testGroupUuid);

        assertEquals(testGroupName1, group.getName());

    }

    @Test
    public void testAddTextComment() {

        long time = System.currentTimeMillis();

        String testText = "testAddTextComment";

        testAddGroup();

        UserComment userComment = new TextComment(new User(), time, testText);

        groupDataSource.insertUserComment(testGroupUuid, userComment);

        PrivateGroup group = groupDataSource.getGroupByUUID(testGroupUuid);

        List<UserComment> userComments = group.getUserComments();

        assertEquals(1, userComments.size());

        TextComment result = (TextComment) userComments.get(0);

        assertEquals(testText, result.getText());

    }


}
