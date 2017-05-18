package de.troido.crowdroutesdk.ns

import com.squareup.moshi.FromJson

internal class NSEntryAdapter {
    @FromJson fun fromJson(json: NSEntryJson): NSEntry =
            NSEntry(
                    json.id,
                    json.url,
                    System.currentTimeMillis() + NSLookupTable.DURATION
            )
}
