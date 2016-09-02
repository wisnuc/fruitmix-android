package com.winsun.fruitmix;

import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.parser.LocalDataParser;
import com.winsun.fruitmix.parser.MediaDataParserFactory;
import com.winsun.fruitmix.parser.ParserFactory;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.MediaShareDataParserFactory;
import com.winsun.fruitmix.parser.UserDataParserFactory;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ParserFactoryUnitTest {


    @Test
    public void createShareParserFactoryTest() {

        ParserFactory<MediaShare> factory = new MediaShareDataParserFactory();

        LocalDataParser<MediaShare> parser = factory.createLocalDataParser();

        assertNotNull(parser);

        RemoteDataParser<MediaShare> remoteDataParser = factory.createRemoteDataParser();

        assertNotNull(remoteDataParser);

    }

    @Test
    public void createMediaParserFactoryTest() {

        ParserFactory<Media> factory = new MediaDataParserFactory();

        LocalDataParser<Media> parser = factory.createLocalDataParser();

        assertNotNull(parser);

        RemoteDataParser<Media> remoteDataParser = factory.createRemoteDataParser();

        assertNotNull(remoteDataParser);

    }

    @Test
    public void createUserParserFactoryTest() {

        ParserFactory<User> factory = new UserDataParserFactory();

        LocalDataParser<User> parser = factory.createLocalDataParser();

        assertNotNull(parser);

        RemoteDataParser<User> remoteDataParser = factory.createRemoteDataParser();

        assertNotNull(remoteDataParser);

    }




}