package dev.pkgh.bot.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.pkgh.bot.users.BotUser;
import dev.pkgh.bot.util.SQLUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import xyz.winston.tcommons.databases.sql.SQLDatabase;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author winston
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class UsersSqlHandler {

    public static UsersSqlHandler IMP = new UsersSqlHandler();

    // --------------------------------------------------

    String LOAD_USER = "SELECT * FROM `Users` WHERE `Id` = ?;";
    String UPDATE_USER = "UPDATE `Users` SET `Group` = ?, `Permissions` = ?, `Reg_Date` = ? WHERE `Id` = ?;";
    String CREATE_USER = "INSERT INTO `Users` (`Id`, `Group`, `Permissions`, `Reg_Date`) VALUES (?, ?, ?, ?);";

    /**
     * Cache is required to save performance by limiting requests
     * to le database ;)
     */
    Cache<Integer, BotUser> usersIdCache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .concurrencyLevel(4)
            .build();

    // --------------------------------------------------

    SQLDatabase database = SQLUtil.getDatabase("PKGHBot");

    /**
     * Get bot user data class by id. Required for {@link BotUser} inner methods.
     * use {@link BotUser#getOrCreate(long)} if you need to get instance of bot user.
     *
     * @param userId    id of user
     * @return          bot user data class instance
     */
    public @NotNull BotUser getBotUser(final long userId) {

        val cacheAsMap = usersIdCache.asMap();

        //чекаем если в кеше уже есть этот игрок
        //просто по ключу получить не можем - кэш не case insensitive
        val cachedEntry = cacheAsMap.entrySet().stream()
                    .filter(entry -> entry.getKey() == userId)
                    .findFirst()
                    .orElse(null);

        if (cachedEntry != null) {
            return cachedEntry.getValue();
        }

        return database.executeQuery(LOAD_USER, userId).thenApply(rows -> {
            // UNSAFE INTERNALS. Access: 0x1002 / ASM dump:

//              private synthetic lambda$getBotUser$1(JLxyz/winston/tcommons/databases/sql/BufferedQuery;)Ldev/pkgh/bot/users/BotUser;
//                 L0
//                  LINENUMBER 65 L0
//                  ALOAD 3
//                  INVOKEVIRTUAL xyz/winston/tcommons/databases/sql/BufferedQuery.size ()I
//                  ICONST_1
//                  IF_ICMPGE L1
//                 L2
//                  LINENUMBER 66 L2
//                  NEW dev/pkgh/bot/users/BotUser
//                  DUP
//                  LLOAD 1
//                  INVOKESPECIAL dev/pkgh/bot/users/BotUser.<init> (J)V
//                  ASTORE 4
//                 L3
//                  LINENUMBER 68 L3
//                  ALOAD 0
//                  ALOAD 4
//                  INVOKEVIRTUAL dev/pkgh/bot/data/UsersSqlHandler.saveUserData (Ldev/pkgh/bot/users/BotUser;)V
//                 L4
//                  LINENUMBER 70 L4
//                  ALOAD 4
//                  ARETURN

            if (rows.size() < 1) {
                val dummy = new BotUser(userId);

                // Registration of user, just by setting it's reg time
                dummy.setRegistrationDate(LocalDateTime.now());

                // Creating user record, to avoid unwanted errors
                createUserRecord(dummy);

                return dummy; // raw dummy bot user
            }

            val row = rows.getFirst();

            val botUser = new BotUser(userId);
            botUser.setGroup(row.getString("Group").orElse(null));
            botUser.setRegistrationDate(LocalDateTime.parse(row.getRequiredString("Reg_Date")));

            return botUser;
        }).join();
    }

    /**
     * Check if user registered in our database
     * @param userId    id of user
     * @return          real???? or FAKE?!?!
     */
    public boolean isUserExists(final long userId) {
        return getBotUser(userId).getRegistrationDate() == null;
    }

    /**
     * Saves data of user to database
     * @param userData  user data class instance
     */
    public void saveUserData(final @NonNull BotUser userData) {
        database.executeUpdate(UPDATE_USER, userData.getGroup(), 0, userData.getRegistrationDate().toString(), userData.getId());
    }

    /**
     * Creates user record in database. Do not use out of this class
     * @param userData  user data class instance
     */
    private void createUserRecord(final @NonNull BotUser userData) {
        database.executeUpdate(CREATE_USER,  userData.getId(), userData.getGroup(), 0, userData.getRegistrationDate().toString());
    }

}