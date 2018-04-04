android端Box模块总结：

遗留问题：

1、发送tweet,通过cloud pipe api,只能包含一个本地文件，选择图片界面加了限制

2、不支持跨nas发送tweet，界面入口加了限制

3、nas上没有绑定的local user用户，通过局域网获取media缩略图和原图资源，因为权限检查会获取失败

4、发送包含大量文件的tweet,因为数量问题会出错，应用逻辑暂未处理

5、删除群用户，box message中只有guid，用户信息获取不到，界面逻辑暂时设计为不显示这条message

6、删除群，mqtt没有通知，在现有业务逻辑条件下，不会更新群列表

7、box list，box的last tweet,如果是图片，并不包含metadata

8、box list，刚创建的box的数据里可能不包含last tweet

9、box list，存在获取失败的问题

业务逻辑：

1、更新群属性，last tweet，是在收到mqtt消息后，更新本地数据，更新群内tweet,则是收到mqtt消息后，获取lastRetrieveTweetIndex之后的所有tweet，增量更新本地数据

2、新消息个数计算逻辑：获取新增的tweet中所有其他人发的tweet数，作为未读新消息，用户查看过后，重置为零

3、数据更新机制：界面渲染是只通过本地数据库获取数据，进行渲染，mqtt收到消息后判断出有新tweet,进而触发获取新tweet,并保存到本地数据库中，再发送消息，界面收到后从本地数据库获取数据，进行渲染

4、tweet草稿箱机制：发送tweet时，先在本地数据库中插入一条草稿，然后触发http请求，界面渲染时合并草稿箱里的tweet和station上tweet进行渲染

5、用户在其他客户端退出群，本客户端获取box list时判断出此情况，会将此box设为只读

6、tweet列表，排序是用tweet存储到数据库里的时间进行排序，如果时间一样，则比较index

7、box 列表，排序是用last tweet的时间排序，如果没有last tweet,则比较modify time