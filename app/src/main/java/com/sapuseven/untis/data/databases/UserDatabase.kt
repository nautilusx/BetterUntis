package com.sapuseven.untis.data.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.UserDatabaseQueryHelper.generateCreateTable
import com.sapuseven.untis.helpers.UserDatabaseQueryHelper.generateDropTable
import com.sapuseven.untis.helpers.UserDatabaseQueryHelper.generateValues
import com.sapuseven.untis.interfaces.TableModel
import com.sapuseven.untis.models.untis.MasterData
import com.sapuseven.untis.models.untis.Settings
import com.sapuseven.untis.models.untis.UserData
import com.sapuseven.untis.models.untis.masterdata.*

private const val DATABASE_VERSION = 2
private const val DATABASE_NAME = "userdata.db"

class UserDatabase private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
	companion object {
		const val COLUMN_NAME_USER_ID = "_user_id"

		private var instance: UserDatabase? = null

		fun createInstance(context: Context): UserDatabase {
			return instance ?: UserDatabase(context)
		}
	}

	override fun onCreate(db: SQLiteDatabase) {
		db.execSQL(UserDatabaseContract.Users.SQL_CREATE_ENTRIES_V2)

		db.execSQL(generateCreateTable<AbsenceReason>())
		db.execSQL(generateCreateTable<Department>())
		db.execSQL(generateCreateTable<Duty>())
		db.execSQL(generateCreateTable<EventReason>())
		db.execSQL(generateCreateTable<EventReasonGroup>())
		db.execSQL(generateCreateTable<ExcuseStatus>())
		db.execSQL(generateCreateTable<Holiday>())
		db.execSQL(generateCreateTable<Klasse>())
		db.execSQL(generateCreateTable<Room>())
		db.execSQL(generateCreateTable<Subject>())
		db.execSQL(generateCreateTable<Teacher>())
		db.execSQL(generateCreateTable<TeachingMethod>())
		db.execSQL(generateCreateTable<SchoolYear>())
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		var currentVersion = oldVersion

		while (currentVersion < newVersion) {
			when (currentVersion) {
				1 -> {
					db.execSQL("ALTER TABLE ${UserDatabaseContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseContract.Users.TABLE_NAME}_v1")
					db.execSQL(UserDatabaseContract.Users.SQL_CREATE_ENTRIES_V2)
					db.execSQL("INSERT INTO ${UserDatabaseContract.Users.TABLE_NAME} SELECT _id, apiUrl, NULL, user, ${UserDatabaseContract.Users.TABLE_NAME}_v1.\"key\", anonymous, timeGrid, masterDataTimestamp, userData, settings, time_created FROM ${UserDatabaseContract.Users.TABLE_NAME}_v1;")
					db.execSQL("DROP TABLE ${UserDatabaseContract.Users.TABLE_NAME}_v1")
				}
			}

			currentVersion++
		}
	}

	fun resetDatabase(db: SQLiteDatabase) {
		db.execSQL(UserDatabaseContract.Users.SQL_DELETE_ENTRIES)

		db.execSQL(generateDropTable<AbsenceReason>())
		db.execSQL(generateDropTable<Department>())
		db.execSQL(generateDropTable<Duty>())
		db.execSQL(generateDropTable<EventReason>())
		db.execSQL(generateDropTable<EventReasonGroup>())
		db.execSQL(generateDropTable<ExcuseStatus>())
		db.execSQL(generateDropTable<Holiday>())
		db.execSQL(generateDropTable<Klasse>())
		db.execSQL(generateDropTable<Room>())
		db.execSQL(generateDropTable<Subject>())
		db.execSQL(generateDropTable<Teacher>())
		db.execSQL(generateDropTable<TeachingMethod>())
		db.execSQL(generateDropTable<SchoolYear>())
	}

	fun addUser(user: User): Long? {
		val db = writableDatabase

		val values = ContentValues()
		values.put(UserDatabaseContract.Users.COLUMN_NAME_APIURL, user.apiUrl)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_SCHOOL_ID, user.schoolId)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_USER, user.user)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_KEY, user.key)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS, user.anonymous)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID, getJSON().stringify(TimeGrid.serializer(), user.timeGrid))
		values.put(UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP, user.masterDataTimestamp)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_USERDATA, getJSON().stringify(UserData.serializer(), user.userData))
		values.put(UserDatabaseContract.Users.COLUMN_NAME_SETTINGS, getJSON().stringify(Settings.serializer(), user.settings))

		val id = db.insert(UserDatabaseContract.Users.TABLE_NAME, null, values)

		db.close()

		return if (id == -1L)
			null
		else
			id
	}

	fun editUser(user: User): Long? {
		val db = writableDatabase

		val values = ContentValues()
		values.put(UserDatabaseContract.Users.COLUMN_NAME_APIURL, user.apiUrl)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_SCHOOL_ID, user.schoolId)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_USER, user.user)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_KEY, user.key)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS, user.anonymous)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID, getJSON().stringify(TimeGrid.serializer(), user.timeGrid))
		values.put(UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP, user.masterDataTimestamp)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_USERDATA, getJSON().stringify(UserData.serializer(), user.userData))
		values.put(UserDatabaseContract.Users.COLUMN_NAME_SETTINGS, getJSON().stringify(Settings.serializer(), user.settings))

		db.update(UserDatabaseContract.Users.TABLE_NAME, values, BaseColumns._ID + "=?", arrayOf(user.id.toString()))
		db.close()

		return user.id
	}

	fun deleteUser(userId: Long) {
		val db = writableDatabase
		db.delete(UserDatabaseContract.Users.TABLE_NAME, BaseColumns._ID + "=?", arrayOf(userId.toString()))
		db.close()
	}

	fun getUser(id: Long): User? {
		val db = this.readableDatabase

		val cursor = db.query(
				UserDatabaseContract.Users.TABLE_NAME,
				arrayOf(
						UserDatabaseContract.Users.COLUMN_NAME_APIURL,
						UserDatabaseContract.Users.COLUMN_NAME_SCHOOL_ID,
						UserDatabaseContract.Users.COLUMN_NAME_USER,
						UserDatabaseContract.Users.COLUMN_NAME_KEY,
						UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS,
						UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID,
						UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP,
						UserDatabaseContract.Users.COLUMN_NAME_USERDATA,
						UserDatabaseContract.Users.COLUMN_NAME_SETTINGS,
						UserDatabaseContract.Users.COLUMN_NAME_CREATED
				),
				BaseColumns._ID + "=?",
				arrayOf(id.toString()), null, null, UserDatabaseContract.Users.COLUMN_NAME_CREATED + " DESC")

		if (!cursor.moveToFirst())
			return null

		val user = User(
				id,
				cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_APIURL)),
				cursor.getInt(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_SCHOOL_ID)),
				cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_USER)),
				cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_KEY)),
				cursor.getInt(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS)) == 1,
				getJSON().parse(TimeGrid.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID))),
				cursor.getLong(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP)),
				getJSON().parse(UserData.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_USERDATA))),
				getJSON().parse(Settings.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_SETTINGS))),
				cursor.getLong(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_CREATED))
		)

		cursor.close()
		db.close()

		return user
	}

	fun getAllUsers(): List<User> {
		val users = ArrayList<User>()
		val db = this.readableDatabase

		val cursor = db.query(
				UserDatabaseContract.Users.TABLE_NAME,
				arrayOf(
						BaseColumns._ID,
						UserDatabaseContract.Users.COLUMN_NAME_APIURL,
						UserDatabaseContract.Users.COLUMN_NAME_SCHOOL_ID,
						UserDatabaseContract.Users.COLUMN_NAME_USER,
						UserDatabaseContract.Users.COLUMN_NAME_KEY,
						UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS,
						UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID,
						UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP,
						UserDatabaseContract.Users.COLUMN_NAME_USERDATA,
						UserDatabaseContract.Users.COLUMN_NAME_SETTINGS,
						UserDatabaseContract.Users.COLUMN_NAME_CREATED
				), null, null, null, null, UserDatabaseContract.Users.COLUMN_NAME_CREATED + " DESC")

		if (cursor.moveToFirst()) {
			do {
				users.add(User(
						cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)),
						cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_APIURL)),
						cursor.getInt(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_SCHOOL_ID)),
						cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_USER)),
						cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_KEY)),
						cursor.getInt(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS)) == 1,
						getJSON().parse(TimeGrid.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID))),
						cursor.getLong(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP)),
						getJSON().parse(UserData.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_USERDATA))),
						getJSON().parse(Settings.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_SETTINGS))),
						cursor.getLong(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_CREATED))
				))
			} while (cursor.moveToNext())
		}

		cursor.close()
		db.close()

		return users
	}

	fun getUsersCount(): Int {
		val db = this.readableDatabase

		val cursor = db.query(
				UserDatabaseContract.Users.TABLE_NAME,
				arrayOf(BaseColumns._ID), null, null, null, null, null)

		val count = cursor.count
		cursor.close()
		db.close()

		return count
	}

	fun setAdditionalUserData(
			userId: Long,
			masterData: MasterData
	) {
		val db = writableDatabase
		db.beginTransaction()

		listOf(
				AbsenceReason.TABLE_NAME to masterData.absenceReasons,
				Department.TABLE_NAME to masterData.departments,
				Duty.TABLE_NAME to masterData.duties,
				EventReason.TABLE_NAME to masterData.eventReasons,
				EventReasonGroup.TABLE_NAME to masterData.eventReasonGroups,
				ExcuseStatus.TABLE_NAME to masterData.excuseStatuses,
				Holiday.TABLE_NAME to masterData.holidays,
				Klasse.TABLE_NAME to masterData.klassen,
				Room.TABLE_NAME to masterData.rooms,
				Subject.TABLE_NAME to masterData.subjects,
				Teacher.TABLE_NAME to masterData.teachers,
				TeachingMethod.TABLE_NAME to masterData.teachingMethods,
				SchoolYear.TABLE_NAME to masterData.schoolyears
		).forEach { refreshAdditionalUserData(db, userId, it.first, it.second) }

		val values = ContentValues()
		values.put(UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP, masterData.timeStamp)
		db.update(
				UserDatabaseContract.Users.TABLE_NAME,
				values,
				BaseColumns._ID + "=?",
				arrayOf(userId.toString()))

		db.setTransactionSuccessful()
		db.endTransaction()
		db.close()
	}

	private fun refreshAdditionalUserData(db: SQLiteDatabase, userId: Long, tableName: String, items: List<TableModel>) {
		db.delete(tableName, "$COLUMN_NAME_USER_ID=?", arrayOf(userId.toString()))
		items.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) }
	}


	inline fun <reified T : TableModel> getAdditionalUserData(userId: Long, table: TableModel): Map<Int, T>? {
		val db = readableDatabase

		val cursor = db.query(
				table.tableName,
				table.generateValues().keySet().toTypedArray(), "$COLUMN_NAME_USER_ID=?",
				arrayOf(userId.toString()), null, null, "id DESC")

		if (!cursor.moveToFirst())
			return null

		val result = mutableMapOf<Int, T>()

		if (cursor.moveToFirst()) {
			do {
				val data = table.parseCursor(cursor) as T
				result[(data as TableModel).elementId] = data
			} while (cursor.moveToNext())
		}

		cursor.close()
		db.close()

		return result.toMap()
	}

	class User(
			val id: Long? = null,
			val apiUrl: String? = null,
			val schoolId: Int,
			val user: String? = null,
			val key: String? = null,
			val anonymous: Boolean = false,
			val timeGrid: TimeGrid,
			val masterDataTimestamp: Long,
			val userData: UserData,
			val settings: Settings,
			val created: Long? = null
	)
}
