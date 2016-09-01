package com.winsun.fruitmix;

import android.database.Cursor;

import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.Share;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.parser.LocalDataParser;
import com.winsun.fruitmix.parser.MediaDataParserFactory;
import com.winsun.fruitmix.parser.ParserFactory;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.ShareDataParserFactory;
import com.winsun.fruitmix.parser.UserDataParserFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ParserFactoryUnitTest {


    @Test
    public void createShareParserFactoryTest() {

        ParserFactory<Share> factory = new ShareDataParserFactory();

        LocalDataParser<Share> parser = factory.createLocalDataParser();

        Cursor cursor = Mockito.mock(Cursor.class);

        List<Share> shares = parser.parse(cursor);

        assertNotNull(shares);

        RemoteDataParser<Share> remoteDataParser = factory.createRemoteDataParser();

        String json = "";

        shares = remoteDataParser.parse(json);

        assertNotNull(shares);

    }

    @Test
    public void createMediaParserFactoryTest() {

        ParserFactory<Media> factory = new MediaDataParserFactory();

        LocalDataParser<Media> parser = factory.createLocalDataParser();

        Cursor cursor = Mockito.mock(Cursor.class);

        List<Media> shares = parser.parse(cursor);

        assertNotNull(shares);

        RemoteDataParser<Media> remoteDataParser = factory.createRemoteDataParser();

        String json = "";

        shares = remoteDataParser.parse(json);

        assertNotNull(shares);

    }

    @Test
    public void createUserParserFactoryTest() {

        ParserFactory<User> factory = new UserDataParserFactory();

        LocalDataParser<User> parser = factory.createLocalDataParser();

        Cursor cursor = Mockito.mock(Cursor.class);

        List<User> shares = parser.parse(cursor);

        assertNotNull(shares);

        RemoteDataParser<User> remoteDataParser = factory.createRemoteDataParser();

        String json = "";

        shares = remoteDataParser.parse(json);

        assertNotNull(shares);

    }




}