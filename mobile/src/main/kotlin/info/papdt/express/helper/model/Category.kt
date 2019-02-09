package info.papdt.express.helper.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
        @PrimaryKey var title: String,
        @ColumnInfo(name = "icon_name") var iconCode: String? = null
) : Parcelable {

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<Category> {
            override fun createFromParcel(parcel: Parcel): Category {
                return Category(parcel)
            }

            override fun newArray(size: Int): Array<Category?> {
                return arrayOfNulls(size)
            }
        }

    }

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(title)
        writeString(iconCode)
    }

    override fun describeContents(): Int = 0

}