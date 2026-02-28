package com.kivoa.controlhub.data

import com.google.gson.annotations.SerializedName

data class AmazonSyncRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("mrp") val mrp: Double? = null,
    @SerializedName("weight") val weight: Int? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("dimensions") val dimensions: Dimensions? = null,
    @SerializedName("stones_data") val stonesData: List<StoneData>? = null,
    @SerializedName("gem_types") val gemTypes: List<String>? = null
)

data class StoneData(
    @SerializedName("type") val type: String,
    @SerializedName("creation_method") val creationMethod: String = "Simulated",
    @SerializedName("treatment_method") val treatmentMethod: String = "Not Treated"
)
